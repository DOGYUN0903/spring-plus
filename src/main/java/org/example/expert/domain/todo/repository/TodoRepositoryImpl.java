package org.example.expert.domain.todo.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.dto.request.SearchConditionRequest;
import org.example.expert.domain.todo.dto.response.QSearchResultResponse;
import org.example.expert.domain.todo.dto.response.SearchResultResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.Optional;

import static org.example.expert.domain.comment.entity.QComment.*;
import static org.example.expert.domain.manager.entity.QManager.*;
import static org.example.expert.domain.todo.entity.QTodo.*;
import static org.example.expert.domain.user.entity.QUser.*;
import static org.springframework.util.StringUtils.*;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(@Param("todoId") Long todoId) {
        Todo findTodo = queryFactory.selectFrom(todo) // todo 에서
                .leftJoin(todo.user, user).fetchJoin() // fetch join
                .where(todo.id.eq(todoId))
                .fetchOne();
        return Optional.ofNullable(findTodo);
    }

    @Override
    public Page<SearchResultResponse> search(SearchConditionRequest condition, Pageable pageable) {

        BooleanBuilder whereClause = buildSearchCondition(condition);

        List<SearchResultResponse> result = queryFactory
                .select(new QSearchResultResponse(
                        todo.title,
                        manager.id.count().as("managerCount"),
                        comment.id.count().as("commentCount")
                ))
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(todo.comments, comment)
                .leftJoin(manager.user, user)
                .where(whereClause)
                .groupBy(todo.id, todo.title)
                .orderBy(todo.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(todo.countDistinct())
                .from(todo)
                .leftJoin(todo.managers, manager)
                .leftJoin(manager.user, user)
                .where(whereClause);

        return PageableExecutionUtils.getPage(result, pageable, countQuery::fetchOne);
    }

    private static BooleanBuilder buildSearchCondition(SearchConditionRequest condition) {
        BooleanBuilder whereClause = new BooleanBuilder();
        if (hasText(condition.getKeyword())) {
            whereClause.and(todo.title.containsIgnoreCase(condition.getKeyword()));
        }
        if (hasText(condition.getManagerNickname())) {
            whereClause.and(manager.user.nickname.containsIgnoreCase(condition.getManagerNickname()));
        }
        if (condition.getStartDate() != null) {
            whereClause.and(todo.createdAt.goe(condition.getStartDate().atStartOfDay()));
        }
        if (condition.getEndDate() != null) {
            whereClause.and(todo.createdAt.loe(condition.getEndDate().atTime(23, 59, 59)));
        }
        return whereClause;
    }

}
