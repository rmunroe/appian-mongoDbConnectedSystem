package com.appiancorp.solutionsconsulting.cs.mongodb.Operations;

import com.mongodb.client.model.Collation;

import java.util.HashMap;
import java.util.Map;

public interface Operation {
    public abstract Map<String, Object> getRequestDiagnostic();
}
