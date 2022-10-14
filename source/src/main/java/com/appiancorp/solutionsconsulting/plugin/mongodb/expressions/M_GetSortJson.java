package com.appiancorp.solutionsconsulting.plugin.mongodb.expressions;

import com.appiancorp.solutionsconsulting.plugin.mongodb.MongoDbCategory;
import com.appiancorp.suiteapi.common.paging.PagingInfo;
import com.appiancorp.suiteapi.common.paging.SortInfo;
import com.appiancorp.suiteapi.expression.annotations.Function;
import com.appiancorp.suiteapi.expression.annotations.Parameter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;


@MongoDbCategory
public class M_GetSortJson {
    @Function
    public String m_GetSortJson(@Parameter(required = false) PagingInfo pagingInfo) {
        if (pagingInfo == null) return null;

        List<SortInfo> sort = pagingInfo.getSort();
        List<String> sorts = new ArrayList<>();

        if (sort.isEmpty()) return null;

        pagingInfo.getSort().forEach(sortInfo -> {
            String asc = sortInfo.isAscending() ? "1" : "-1";
            sorts.add(MessageFormat.format("\"{0}\": {1}", sortInfo.getField(), asc));
        });

        return "{" + String.join(", ", sorts) + "}";
    }
}
