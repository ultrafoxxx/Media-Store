package com.holzhausen.mediastore;

import android.content.Context;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.holzhausen.mediastore.daos.MultimediaItemDao;
import com.holzhausen.mediastore.daos.MultimediaItemTagCrossRefDao;
import com.holzhausen.mediastore.daos.TagDao;
import com.holzhausen.mediastore.databases.AppDatabase;
import com.holzhausen.mediastore.model.MultimediaItem;
import com.holzhausen.mediastore.model.MultimediaItemTagCrossRef;
import com.holzhausen.mediastore.model.MultimediaItemsTags;
import com.holzhausen.mediastore.model.MultimediaType;
import com.holzhausen.mediastore.model.Tag;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RunWith(AndroidJUnit4.class)
public class DBTests {

  private MultimediaItemDao multimediaItemDao;
  private TagDao tagDao;
  private MultimediaItemTagCrossRefDao multimediaItemTagCrossRefDao;
  private  AppDatabase db;

  private final MultimediaItem[] multimediaItems = {
          new MultimediaItem("file1", "file1", MultimediaType.IMAGE, false),
          new MultimediaItem("file2", "file2", MultimediaType.IMAGE, true),
          new MultimediaItem("file3", "file3", MultimediaType.VIDEO, false),
          new MultimediaItem("file4", "file4", MultimediaType.VOICE_RECORDING, false)
  };

  private final Tag[] tags = {
          new Tag("tag1"),
          new Tag("tag2"),
          new Tag("tag3"),
          new Tag("tag4")
  };

  private final MultimediaItemTagCrossRef[] crossRefs = {
          new MultimediaItemTagCrossRef("file1", "tag1"),
          new MultimediaItemTagCrossRef("file1", "tag2"),
          new MultimediaItemTagCrossRef("file2", "tag2"),
          new MultimediaItemTagCrossRef("file3", "tag3"),
          new MultimediaItemTagCrossRef("file3", "tag4")
  };

  @Rule
  public InstantTaskExecutorRule taskExecutor = new InstantTaskExecutorRule();

  @Before
  public void createDb() {
      Context context = ApplicationProvider.getApplicationContext();
      db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
              .allowMainThreadQueries()
              .build();
      multimediaItemDao = db.multimediaItemDao();
      tagDao = db.tagDao();
      multimediaItemTagCrossRefDao = db.multimediaItemTagCrossRefDao();

      multimediaItemDao.insert(multimediaItems).blockingAwait();

      tagDao.insert(tags).blockingAwait();
      
      multimediaItemTagCrossRefDao.insert(crossRefs).blockingAwait();
      
  }

  @After
  public void closeDb() throws IOException {
      db.close();
  }

  @Test
  public void testQueryingAllItems() {
      final List<MultimediaItemsTags> expectedResult = Arrays.stream(multimediaItems)
              .map(multimediaItem -> {
                  final MultimediaItemsTags itemsTags = new MultimediaItemsTags();
                  itemsTags.setMultimediaItem(multimediaItem);
                  itemsTags.setTags(
                          Arrays.stream(crossRefs)
                                  .filter(crossRef -> crossRef.getFileName().equals(multimediaItem.getFileName()))
                          .map(crossRef -> new Tag(crossRef.getTagName()))
                          .collect(Collectors.toList())
                  );
                  return itemsTags;
              })
              .collect(Collectors.toList());

      multimediaItemDao.getAll().test().assertValue(value -> value.equals(expectedResult));
  }

    @Test
    public void testQueryingItemsByQuery() {
      String query = "tag2";
      final List<MultimediaItemsTags> expectedResult = Arrays.stream(multimediaItems)
              .map(multimediaItem -> {
                  final MultimediaItemsTags itemsTags = new MultimediaItemsTags();
                  itemsTags.setMultimediaItem(multimediaItem);
                  itemsTags.setTags(
                          Arrays.stream(crossRefs)
                                  .filter(crossRef -> crossRef.getFileName().equals(multimediaItem.getFileName()))
                                  .map(crossRef -> new Tag(crossRef.getTagName()))
                                  .collect(Collectors.toList())
                  );
                  return itemsTags;
              })
              .filter(multimediaItemsTags -> multimediaItemsTags
                      .getTags()
                      .stream()
                      .anyMatch(tag -> tag.getTagName().equals(query)))
              .collect(Collectors.toList());

      multimediaItemDao.queryItemsByNamesAndTags(query).test().assertValue(value -> value.equals(expectedResult));
    }

    @Test
    public void testCheckingIfItemExists() {
        String titleName = "file1";
        int expectedResult = 1;

        multimediaItemDao.numberOfItemsWithProvidedFileName(titleName)
                .test()
                .assertValue(value -> value == expectedResult);
    }

}
