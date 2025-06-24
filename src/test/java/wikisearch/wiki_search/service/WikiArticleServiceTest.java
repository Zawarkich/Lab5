package wikisearch.wiki_search.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import wikisearch.wiki_search.cache.SimpleCache;
import wikisearch.wiki_search.dto.WikiArticleDto;
import wikisearch.wiki_search.entity.WikiArticle;
import wikisearch.wiki_search.repository.WikiArticleRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WikiArticleServiceTest {
    @Mock
    private WikiArticleRepository articleRepo;
    @Mock
    private SimpleCache cache;
    @InjectMocks
    private WikiArticleService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new WikiArticleService(articleRepo, cache);
    }

    @Test
    void testCreateArticle() {
        WikiArticle article = mock(WikiArticle.class);
        WikiArticle saved = mock(WikiArticle.class);
        WikiArticleDto dtoMock = mock(WikiArticleDto.class);
        when(articleRepo.save(article)).thenReturn(saved);
        when(saved.getId()).thenReturn(1L);
        when(saved.getTitle()).thenReturn("title");
        when(saved.getContent()).thenReturn("content");
        WikiArticleDto dto = service.createArticle(article);
        assertEquals("title", dto.getTitle());
        assertEquals("content", dto.getContent());
        assertNotNull(dto.getId());
    }

    @Test
    void testCreateArticlesBulk() {
        WikiArticle a1 = mock(WikiArticle.class);
        WikiArticle a2 = mock(WikiArticle.class);
        WikiArticle s1 = mock(WikiArticle.class);
        WikiArticle s2 = mock(WikiArticle.class);
        when(articleRepo.save(a1)).thenReturn(s1);
        when(articleRepo.save(a2)).thenReturn(s2);
        when(s1.getId()).thenReturn(1L);
        when(s1.getTitle()).thenReturn("t1");
        when(s1.getContent()).thenReturn("c1");
        when(s2.getId()).thenReturn(2L);
        when(s2.getTitle()).thenReturn("t2");
        when(s2.getContent()).thenReturn("c2");
        List<WikiArticleDto> dtos = service.createArticlesBulk(Arrays.asList(a1, a2));
        assertEquals(2, dtos.size());
        assertEquals("t1", dtos.get(0).getTitle());
        assertEquals("t2", dtos.get(1).getTitle());
    }

    @Test
    void testGetAllArticles() {
        WikiArticle a1 = mock(WikiArticle.class);
        WikiArticle a2 = mock(WikiArticle.class);
        when(a1.getId()).thenReturn(1L);
        when(a1.getTitle()).thenReturn("t1");
        when(a1.getContent()).thenReturn("c1");
        when(a2.getId()).thenReturn(2L);
        when(a2.getTitle()).thenReturn("t2");
        when(a2.getContent()).thenReturn("c2");
        when(cache.get("all_articles")).thenReturn(null);
        when(articleRepo.findAll()).thenReturn(Arrays.asList(a1, a2));
        List<WikiArticleDto> dtos = service.getAllArticles();
        assertEquals(2, dtos.size());
    }

    @Test
    void testGetArticleById() {
        WikiArticle a1 = mock(WikiArticle.class);
        when(a1.getId()).thenReturn(1L);
        when(a1.getTitle()).thenReturn("t1");
        when(a1.getContent()).thenReturn("c1");
        when(cache.get("article:1")).thenReturn(null);
        when(articleRepo.findById(1L)).thenReturn(Optional.of(a1));
        WikiArticleDto dto = service.getArticleById(1L);
        assertEquals("t1", dto.getTitle());
    }

    @Test
    void testUpdateArticle() {
        WikiArticle a1 = mock(WikiArticle.class);
        WikiArticle saved = mock(WikiArticle.class);
        when(articleRepo.save(a1)).thenReturn(saved);
        when(saved.getId()).thenReturn(1L);
        when(saved.getTitle()).thenReturn("t1");
        when(saved.getContent()).thenReturn("c1");
        WikiArticleDto dto = service.updateArticle(1L, a1);
        assertEquals(1L, dto.getId());
    }

    @Test
    void testDeleteArticle() {
        doNothing().when(articleRepo).deleteById(1L);
        service.deleteArticle(1L);
        verify(articleRepo, times(1)).deleteById(1L);
    }
}
