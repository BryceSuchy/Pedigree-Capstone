package com.pedigreetechnologies.diagnosticview;

import java.util.Comparator;

/**
 * Created by Joe on 2/16/2017.
 */

public class DiagnosticParameterComparator implements Comparator<DiagnosticParameter> {

    public int compare(DiagnosticParameter diagnosticParameter1, DiagnosticParameter diagnosticParameter2) {
        if (diagnosticParameter1.getDataType() - diagnosticParameter2.getDataType() < 0) {
            return -1;
        } else if (diagnosticParameter1.getDataType() - diagnosticParameter2.getDataType() == 0) {
            if (diagnosticParameter1.getParameterID() - diagnosticParameter2.getParameterID() < 0) {
                return -1;
            } else if (diagnosticParameter1.getParameterID() - diagnosticParameter2.getParameterID() == 0) {
                return 0;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }
}

