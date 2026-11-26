package br.edu.ifsp.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Todos os testes de integração")
@SelectPackages({"br.edu.ifsp.persistencia", "br.edu.ifsp.api"})
@IncludeTags("IntegrationTest")

public class SuiteIntegracao {
}