package kilanny.shamarlymushaf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RecoverySystem;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * A fragment representing a single Reciter detail screen.
 * This fragment is either contained in a {@link ReciterListActivity}
 * in two-pane mode (on tablets) or a {@link ReciterDetailActivity}
 * on handsets.
 */
public class ReciterDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";

    private AsyncTask prevTask;
    private ArrayAdapter<SurahDownload> adapter;

    /**
     * prevent user from download/delete single items while
     * download all is running
     */
    private boolean canDoSingleOperation = true;

    /**
     * The dummy content this fragment is presenting.
     */
    String mItem;

    public boolean isDownloadActive() {
        return prevTask != null && !prevTask.isCancelled();
    }

    public void setCanDoSingleOperation(boolean canDoSingleOperation) {
        this.canDoSingleOperation = canDoSingleOperation;
    }

    public void cancelActiveOperations() {
        if (prevTask != null) {
            if (!prevTask.isCancelled())
                prevTask.cancel(true);
        }
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ReciterDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem = getArguments().getString(ARG_ITEM_ID);
        }
    }

    public void setSurahProgress(int surah, int prog) {
        adapter.getItem(surah - 1).downloadedAyah = prog;
        adapter.notifyDataSetChanged();
    }

    private int getSurahAyahCount(int surah) {
        return Utils.AYAH_COUNT[surah - 1] + (surah == 1 ? 1 : 0);
    }

    class SurahDownload {
        public Surah surah;
        public int totalAyah, downloadedAyah;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_reciter_detail, container, false);

        if (mItem != null) {
            rootView.findViewById(R.id.progressBarLoading).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.listview_reciter_detail).setVisibility(View.GONE);
            SurahDownload[] arr = new SurahDownload[WelcomeActivity.surahs.length];
            for (int i = 0; i < WelcomeActivity.surahs.length; ++i) {
                arr[i] = new SurahDownload();
                arr[i].surah = WelcomeActivity.surahs[i];
                arr[i].totalAyah = getSurahAyahCount(i + 1);
            }
            adapter = new ArrayAdapter<SurahDownload>(
                    getActivity(),
                    R.layout.reciter_download_list_item, arr) {
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    LayoutInflater inflater = (LayoutInflater) getActivity()
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View rowView = inflater.inflate(R.layout.reciter_download_list_item,
                            parent, false);
                    TextView s = (TextView) rowView.findViewById(R.id.surahName);
                    final SurahDownload item = adapter.getItem(position);
                    s.setText(item.surah.name);
                    final TextView txt = (TextView) rowView.findViewById(R.id.itemProgressText);
                    final ProgressBar progress = (ProgressBar)
                            rowView.findViewById(R.id.itemProgress);
                    progress.setMax(item.totalAyah);
                    txt.setText(String.format("%d / %d", item.downloadedAyah, item.totalAyah));
                    progress.setProgress(item.downloadedAyah);
                    final ImageButton btn = (ImageButton) rowView.findViewById(R.id.download_item);
                    btn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (!canDoSingleOperation) return;
                            //user can make only one single surah download per time
                            //any other surah is clicked, cancel the previous surah
                            if (prevTask != null) {
                                if (!prevTask.isCancelled())
                                    prevTask.cancel(true);
                                Toast.makeText(getActivity(),
                                        "يتم إيقاف التحميل...", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            prevTask = Utils.downloadSurah(getActivity(), mItem, position + 1,
                                    new RecoverySystem.ProgressListener() {
                                        @Override
                                        public void onProgress(int prog) {
                                            setSurahProgress(position + 1, prog);
                                        }
                                    }, new DownloadTaskCompleteListener() {
                                        @Override
                                        public void taskCompleted(int result) {
                                            prevTask = null;
                                            if (result != Utils.DOWNLOAD_OK && result != Utils.DOWNLOAD_USER_CANCEL) {
                                                Toast.makeText(getActivity(),
                                                        "فشل التحميل. تأكد من اتصالك بالشبكة ووجود مساحة كافية بجهازك",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                            Toast.makeText(getActivity(),
                                    "يتم التحميل...", Toast.LENGTH_SHORT).show();
                        }
                    });
                    rowView.findViewById(R.id.delete_item).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (!canDoSingleOperation) return;
                            if (prevTask != null) return;
                            final String message = String.format("حذف تسجيل سورة %s للقارئ المحدد",
                                    item.surah.name);
                            Utils.showConfirm(getActivity(), "حذف سورة",
                                    "متأكد أنك تريد " + message + " ؟"
                                    , new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final ProgressDialog show = new ProgressDialog(getActivity());
                                            show.setTitle(message);
                                            show.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                            show.setIndeterminate(false);
                                            show.setCancelable(false);
                                            show.setMax(item.totalAyah);
                                            show.setProgress(0);
                                            show.show();
                                            new AsyncTask<Void, Integer, Void>() {
                                                @Override
                                                protected Void doInBackground(Void... params) {
                                                    File surahDir = Utils.getSurahDir(getActivity(),
                                                            mItem, position + 1);
                                                    if (surahDir.exists()) {
                                                        for (int i = 0; i <= item.totalAyah; ++i) {
                                                            File file = new File(Utils.getAyahFile(i, surahDir));
                                                            if (file.exists())
                                                                file.delete();
                                                            publishProgress(i);
                                                        }
                                                    } else publishProgress(item.totalAyah);
                                                    return null;
                                                }

                                                @Override
                                                protected void onProgressUpdate(final Integer... values) {
                                                    show.setProgress(values[0]);
                                                }

                                                @Override
                                                protected void onPostExecute(Void v) {
                                                    setSurahProgress(position + 1, 0);
                                                    show.dismiss();
                                                }
                                            }.execute();
                                        }
                                    }, null);
                        }
                    });
                    return rowView;
                }
            };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 114; ++i) {
                        adapter.getItem(i).downloadedAyah = Utils.getNumDownloaded(getActivity(), mItem, i + 1);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            rootView.findViewById(R.id.progressBarLoading)
                                    .setVisibility(View.GONE);
                            ListView listview = (ListView)
                                    rootView.findViewById(R.id.listview_reciter_detail);
                            listview.setAdapter(adapter);
                            listview.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }).start();
        }

        return rootView;
    }
}