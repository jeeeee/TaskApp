package jp.teckacademy.takeda.ryoji.taskapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by ryojitakeda on 2018/03/09.
 */

public class CategoryActivity extends AppCompatActivity {

    private EditText mCategoryMakeText;
    private Button mCategoryMakeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        mCategoryMakeText = findViewById(R.id.category_make_edit);
        mCategoryMakeButton = findViewById(R.id.category_make_button);

        mCategoryMakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCategory();
                finish();
            }
        });
    }

    private void addCategory() {
        Realm realm = Realm.getDefaultInstance();

        String categoryText = mCategoryMakeText.getText().toString();

        RealmResults<Category> categoryRealmResultsContains =
                realm.where(Category.class)
                        .contains("category", categoryText)
                        .findAll();

        Log.d("TAG", "addCategory: " + categoryRealmResultsContains.size());

        // 同じカテゴリ名がないか確認
        if (categoryRealmResultsContains.size() == 0) {
            realm.beginTransaction();

            RealmResults<Category> categoryRealmResults =
                    realm.where(Category.class)
                            .findAll();

            int identifier;
            if (categoryRealmResults.max("id") != null) {
                identifier = categoryRealmResults.max("id").intValue() + 1;
            } else {
                identifier = 1;
                Category category0 = new Category();
                category0.setId(0);
                category0.setCategory("ALL");
                realm.copyToRealm(category0);
            }

            Category category = new Category();
            category.setId(identifier);
            category.setCategory(categoryText);
            realm.copyToRealm(category);
            realm.commitTransaction();

            realm.close();
        }
    }
}




















