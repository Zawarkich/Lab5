package wikisearch.wiki_search.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import wikisearch.wiki_search.cache.SimpleCache;
import wikisearch.wiki_search.dto.WikiArticleDto;
import wikisearch.wiki_search.entity.WikiArticle;
import wikisearch.wiki_search.repository.WikiArticleRepository;

import java.util.List;

@Service
public class WikiArticleService {
    private final WikiArticleRepository articleRepo;
    private final RestTemplate restTemplate;
    private final SimpleCache cache;

    @Autowired
    public WikiArticleService(WikiArticleRepository articleRepo, SimpleCache cache) {
        this.articleRepo = articleRepo;
        this.cache = cache;
        this.restTemplate = new RestTemplate();
    }

    public WikiArticle search(String term) {
        WikiArticle cached = (WikiArticle) cache.get(term);
        if (cached != null) {
            return cached;
        }
        String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&explaintext=true&titles=" + term;
        String response = restTemplate.getForObject(url, String.class);
        String extract;
        if (response != null && response.contains("extract")) {
            extract = response.split("\"extract\":\"")[1].split("\"")[0];
        } else {
            extract = "No results found";
        }
        WikiArticle article = new WikiArticle(term, extract);
        articleRepo.save(article);
        cache.put(term, article);
        return article;
    }

    public List<WikiArticleDto> getAllArticles() {
        @SuppressWarnings("unchecked")
        List<WikiArticleDto> cached = (List<WikiArticleDto>) cache.get("all_articles");
        if (cached != null) {
            return cached;
        }
        List<WikiArticleDto> result = articleRepo.findAll().stream()
            .map(a -> new WikiArticleDto(a.getId(), a.getTitle(), a.getContent()))
            .collect(java.util.stream.Collectors.toList());
        cache.put("all_articles", result);
        return result;
    }

    public WikiArticleDto getArticleById(Long id) {
        WikiArticleDto cached = (WikiArticleDto) cache.get("article:" + id);
        if (cached != null) {
            return cached;
        }
        WikiArticleDto result = articleRepo.findById(id)
            .map(a -> new WikiArticleDto(a.getId(), a.getTitle(), a.getContent()))
            .orElse(null);
        if (result != null) {
            cache.put("article:" + id, result);
        }
        return result;
    }

    public WikiArticleDto createArticle(WikiArticle article) {
        WikiArticleDto cached = (WikiArticleDto) cache.get("create:" + article.getTitle());
        if (cached != null) {
            return cached;
        }
        WikiArticle saved = articleRepo.save(article);
        cache.clear();
        WikiArticleDto result = new WikiArticleDto(saved.getId(), saved.getTitle(), saved.getContent());
        cache.put("create:" + article.getTitle(), result);
        return result;
    }

    public WikiArticleDto updateArticle(Long id, WikiArticle article) {
        WikiArticleDto cached = (WikiArticleDto) cache.get("update:" + id);
        if (cached != null) {
            return cached;
        }
        article.setId(id);
        WikiArticle saved = articleRepo.save(article);
        cache.clear();
        WikiArticleDto result = new WikiArticleDto(saved.getId(), saved.getTitle(), saved.getContent());
        cache.put("update:" + id, result);
        return result;
    }

    public void deleteArticle(Long id) {
        cache.get("delete:" + id);
        cache.clear();
        cache.put("delete:" + id, "deleted");
    }

    public WikiArticleDto searchAndSaveFromWiki(String term) {
        WikiArticleDto cached = (WikiArticleDto) cache.get("searchAndSave:" + term);
        if (cached != null) {
            return cached;
        }
        String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&explaintext=true&titles=" + term;
        String response = new org.springframework.web.client.RestTemplate().getForObject(url, String.class);
        String extract;
        if (response != null && response.contains("extract")) {
            extract = response.split("\"extract\":\"")[1].split("\"")[0];
        } else {
            extract = "No results found";
        }
        WikiArticle article = new WikiArticle(term, extract);
        WikiArticle saved = articleRepo.save(article);
        cache.clear();
        WikiArticleDto result = new WikiArticleDto(saved.getId(), saved.getTitle(), saved.getContent());
        cache.put("searchAndSave:" + term, result);
        return result;
    }

    public List<WikiArticleDto> createArticlesBulk(List<WikiArticle> articles) {
        @SuppressWarnings("unchecked")
        List<WikiArticleDto> cached = (List<WikiArticleDto>) cache.get("bulk_create");
        if (cached != null) {
            return cached;
        }
        List<WikiArticle> saved = articles.stream()
            .map(articleRepo::save)
            .collect(java.util.stream.Collectors.toList());
        cache.clear();
        List<WikiArticleDto> result = saved.stream()
            .map(a -> new WikiArticleDto(a.getId(), a.getTitle(), a.getContent()))
            .collect(java.util.stream.Collectors.toList());
        cache.put("bulk_create", result);
        return result;
    }
}
