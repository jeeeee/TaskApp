package jp.teckacademy.takeda.ryoji.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/*
*
課題：タスク管理アプリの機能追加
タスク管理アプリにCategory（カテゴリ）を追加して、ListViewの画面でカテゴリによるTaskの絞り込みをさせるようにしてください。

下記の要件を満たしてください。

本レッスンで制作した TaskApp プロジェクトを基に制作してください
TaskクラスにcategoryというStringプロパティを追加してください
タスク作成画面でcategoryを入力できるようにしてください
一覧画面に文字列検索用の入力欄を設置し、categoryと合致するTaskのみ絞込み表示させてください
要件を満たすものであれば、どのようなものでも構いません。
例えば、保存ボタンやキャンセルボタンを作ったりしてみてください。
見栄え良く、自分でも使いやすいタスク管理アプリを目指しましょう！

ヒント
以下のRealmのドキュメントを確認しましょう。
検索条件を指定する | Realm

注意
categoryプロパティを追加したあとは、エミュレータのタスク管理アプリを削除してください（以前のデータである *.realm ファイルが残っているため）
発展課題
以下は、チャレンジできる方はしてみましょう。

レッスン内の機能を全て満たしてください（AlarmManager機能などあるかも確認してください）
上記のString型のcategoryを、クラスのCategoryへ変更してください
追加で、タスク作成画面から遷移する画面を1つ作成してください
その画面ではCategory（idとカテゴリ名を持つ）のクラスを作成できるようにしてください
タスク作成画面でTaskを作成するときにCategoryを選択できるようにしてください
一覧画面でCategoryを選択すると、Categoryに属しているタスクのみ表示されるようにしてください
*
* */

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.teckacademy.takeda.ryoji.taskapp.TASK";

    private Realm mRealm;

    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object element) {
            reloadListView();
        }
    };

    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    private AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner) parent;
            Category category = (Category) spinner.getSelectedItem();
            RealmResults<Task> taskRealmResults = null;
            if (category.getId() == 0) {
                taskRealmResults = mRealm.where(Task.class).findAllSorted("date", Sort.DESCENDING);
            } else {
                taskRealmResults = mRealm.where(Task.class).equalTo
                        ("categoryId", category.getId()).findAllSorted("date", Sort.DESCENDING);
            }
            mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
            mListView.setAdapter(mTaskAdapter);
            mTaskAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();

                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();

                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        reloadListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });

        reloadListView();
        reloadCategory();
    }

    private void reloadListView() {
        // Realmデータベースから、「全てのデータを取得して新しい日時順に並べた結果」を取得
        RealmResults<Task> taskRealmResults =
                mRealm.where(Task.class).findAllSorted("date", Sort.DESCENDING);
        // 上記の結果を、TaskList としてセットする
        mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
        // TaskのListView用のアダプタに渡す
        mListView.setAdapter(mTaskAdapter);
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

    private void reloadCategory() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Category> categoryRealmResults = realm.where(Category.class).findAllSorted("id");

        ArrayAdapter<Category> mCategoryAdapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_item, categoryRealmResults);
        mCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner mCategorySpinner = findViewById(R.id.category_spinner);
        mCategorySpinner.setOnItemSelectedListener(onItemSelectedListener);
        mCategorySpinner.setAdapter(mCategoryAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mRealm.close();
    }
}
