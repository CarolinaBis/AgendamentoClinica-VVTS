package br.edu.ifsp.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testes TDD")
@SelectPackages({"br.edu.ifsp.tdd", "br.ifsp.funcional"})
@IncludeTags("TDD")
public class SuiteTdd {
}
