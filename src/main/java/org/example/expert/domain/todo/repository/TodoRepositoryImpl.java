package org.example.expert.domain.todo.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.example.expert.domain.todo.entity.QTodo;
import org.example.expert.domain.todo.entity.Todo;

import java.util.Optional;

import static org.example.expert.domain.user.entity.QUser.*;

@RequiredArgsConstructor
public class TodoRepositoryImpl implements TodoRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Todo> findByIdWithUser(Long todoId) {

        Todo todo = queryFactory.selectFrom(QTodo.todo) // todo 에서
                .leftJoin(QTodo.todo.user, user).fetchJoin() // fetch join
                .where(QTodo.todo.id.eq(todoId))
                .fetchOne();
        return Optional.ofNullable(todo);
    }
}
