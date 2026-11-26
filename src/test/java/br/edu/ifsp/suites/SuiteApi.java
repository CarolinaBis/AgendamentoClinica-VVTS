package br.edu.ifsp.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testes de API")
@SelectPackages("br.edu.ifsp.integracao")
@IncludeTags("ApiTest")
public class SuiteApi {
}
