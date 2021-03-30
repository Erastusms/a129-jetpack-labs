package com.dicoding.academies.data;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.PagedList;

import com.dicoding.academies.data.source.local.LocalDataSource;
import com.dicoding.academies.data.source.local.entity.CourseEntity;
import com.dicoding.academies.data.source.local.entity.CourseWithModule;
import com.dicoding.academies.data.source.local.entity.ModuleEntity;
import com.dicoding.academies.data.source.remote.RemoteDataSource;
import com.dicoding.academies.data.source.remote.response.ContentResponse;
import com.dicoding.academies.data.source.remote.response.CourseResponse;
import com.dicoding.academies.data.source.remote.response.ModuleResponse;
import com.dicoding.academies.utils.AppExecutors;
import com.dicoding.academies.utils.DataDummy;
import com.dicoding.academies.utils.LiveDataTestUtil;
import com.dicoding.academies.utils.PagedListUtil;
import com.dicoding.academies.utils.TestExecutor;
import com.dicoding.academies.vo.Resource;

import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AcademyRepositoryTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private RemoteDataSource remote = mock(RemoteDataSource.class);
    private LocalDataSource local = mock(LocalDataSource.class);
    private Executor executor = new TestExecutor();

    private AppExecutors appExecutors = new AppExecutors(executor, executor, executor);

    private FakeAcademyRepository academyRepository = new FakeAcademyRepository(remote, local, appExecutors);

    private ArrayList<CourseResponse> courseResponses = DataDummy.generateRemoteDummyCourses();
    private String courseId = courseResponses.get(0).getId();
    private ArrayList<ModuleResponse> moduleResponses = DataDummy.generateRemoteDummyModules(courseId);
    private String moduleId = moduleResponses.get(0).getModuleId();
    private ContentResponse content = DataDummy.generateRemoteDummyContent(moduleId);

    @Test
    public void getAllCourses() {
        DataSource.Factory<Integer, CourseEntity> dataSourceFactory = mock(DataSource.Factory.class);
        when(local.getAllCourses()).thenReturn(dataSourceFactory);
        academyRepository.getAllCourses();

        Resource<PagedList<CourseEntity>> courseEntities = Resource.success(PagedListUtil.mockPagedList(DataDummy.generateDummyCourses()));
        verify(local).getAllCourses();
        assertNotNull(courseEntities.data);
        assertEquals(courseResponses.size(), courseEntities.data.size());
    }

    @Test
    public void getAllModulesByCourse() {
        MutableLiveData<List<ModuleEntity>> dummyModules = new MutableLiveData<>();
        dummyModules.setValue(DataDummy.generateDummyModules(courseId));
        when(local.getAllModulesByCourse(courseId)).thenReturn(dummyModules);

        Resource<List<ModuleEntity>> courseEntities = LiveDataTestUtil.getValue(academyRepository.getAllModulesByCourse(courseId));
        verify(local).getAllModulesByCourse(courseId);
        assertNotNull(courseEntities.data);
        assertEquals(moduleResponses.size(), courseEntities.data.size());
    }

    @Test
    public void getBookmarkedCourses() {
        DataSource.Factory<Integer, CourseEntity> dataSourceFactory = mock(DataSource.Factory.class);
        when(local.getBookmarkedCourses()).thenReturn(dataSourceFactory);
        academyRepository.getBookmarkedCourses();

        Resource<PagedList<CourseEntity>> courseEntities = Resource.success(PagedListUtil.mockPagedList(DataDummy.generateDummyCourses()));
        verify(local).getBookmarkedCourses();
        assertNotNull(courseEntities);
        assertEquals(courseResponses.size(), courseEntities.data.size());
    }

    @Test
    public void getContent() {
        MutableLiveData<ModuleEntity> dummyEntity = new MutableLiveData<>();
        dummyEntity.setValue(DataDummy.generateDummyModuleWithContent(moduleId));
        when(local.getModuleWithContent(courseId)).thenReturn(dummyEntity);

        Resource<ModuleEntity> courseEntitiesContent = LiveDataTestUtil.getValue(academyRepository.getContent(courseId));
        verify(local).getModuleWithContent(courseId);
        assertNotNull(courseEntitiesContent);
        assertNotNull(courseEntitiesContent.data.contentEntity);
        assertNotNull(courseEntitiesContent.data.contentEntity.getContent());
        assertEquals(content.getContent(), courseEntitiesContent.data.contentEntity.getContent());
    }


    @Test
    public void getCourseWithModules() {
        MutableLiveData<CourseWithModule> dummyEntity = new MutableLiveData<>();
        dummyEntity.setValue(DataDummy.generateDummyCourseWithModules(DataDummy.generateDummyCourses().get(0), false));
        when(local.getCourseWithModules(courseId)).thenReturn(dummyEntity);

        Resource<CourseWithModule> courseEntities = LiveDataTestUtil.getValue(academyRepository.getCourseWithModules(courseId));
        verify(local).getCourseWithModules(courseId);
        assertNotNull(courseEntities.data);
        assertNotNull(courseEntities.data.mCourse.getTitle());
        assertEquals(courseResponses.get(0).getTitle(), courseEntities.data.mCourse.getTitle());
    }

    @Test
    public void setCourseBookmark() {
        CourseEntity dummy = DataDummy.generateDummyCourses().get(0);

        academyRepository.setCourseBookmark(dummy, true);
        verify(local, times(1)).setCourseBookmark(dummy, true);
    }

    @Test
    public void setReadModule() {
        ModuleEntity dummy = DataDummy.generateDummyModules("a").get(0);

        academyRepository.setReadModule(dummy);
        verify(local, times(1)).setReadModule(dummy);
    }
}