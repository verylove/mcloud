package me.javaroad.blog.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.JPQLQuery;
import java.util.Objects;
import me.javaroad.blog.entity.QLabel;
import me.javaroad.blog.entity.Article;
import me.javaroad.blog.entity.QArticle;
import org.apache.commons.lang3.StringUtils;
import me.javaroad.blog.controller.api.request.ArticleSearchRequest;
import me.javaroad.blog.entity.QCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QueryDslRepositorySupport;

/**
 * @author heyx
 */
public class ArticleRepositoryImpl extends QueryDslRepositorySupport implements ArticleRepositoryCustom {

    private QArticle article = QArticle.article;
    private QCategory category = QCategory.category;
    private QLabel label = QLabel.label;

    public ArticleRepositoryImpl() {
        super(Article.class);
    }


    @Override
    public Page<Article> findBySearchRequest(ArticleSearchRequest searchRequest, Pageable pageable) {
        JPQLQuery<Article> query = from(article)
            .leftJoin(article.categories, category).fetchJoin()
            .leftJoin(article.labels, label).fetchJoin();

        bindSearchParam(query, searchRequest);

        QueryResults<Article> articleQueryResults = getQuerydsl()
            .applyPagination(pageable, query).fetchResults();

        return new PageImpl<>(articleQueryResults.getResults(),
            pageable, articleQueryResults.getTotal());
    }

    @Override
    public Article findByUserAndId(String username, Long articleId) {
        return from(article)
            .leftJoin(article.categories, category).fetchJoin()
            .leftJoin(article.labels, label).fetchJoin()
            .where(article.id.eq(articleId), article.author.username.eq(username))
            .fetchOne();
    }

    private void bindSearchParam(JPQLQuery<Article> query, ArticleSearchRequest searchRequest) {
        if (StringUtils.isNotBlank(searchRequest.getUsername())) {
            query.where(article.author.username.eq(searchRequest.getUsername()));
        }
        if (Objects.nonNull(searchRequest.getCategoryId())) {
            query.where(category.id.eq(searchRequest.getCategoryId()));
        }
        if (Objects.nonNull(searchRequest.getLabelId())) {
            query.where(label.id.eq(searchRequest.getLabelId()));
        }
    }
}
