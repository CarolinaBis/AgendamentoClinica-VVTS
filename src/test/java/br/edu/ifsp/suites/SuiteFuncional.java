package br.edu.ifsp.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testes funcionais")
@SelectPackages({"br.edu.ifsp.tdd", "br.edu.ifsp.funcional"})
@IncludeTags("Functional")
public class SuiteFuncional {
}
