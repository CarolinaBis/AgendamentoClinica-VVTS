package br.edu.ifsp.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testes de interface (UI)")
@SelectPackages("br.edu.ifsp.ui")
@IncludeTags("UiTest")

public class SuiteUi {
}