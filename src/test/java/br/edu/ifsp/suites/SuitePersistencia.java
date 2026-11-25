package br.edu.ifsp.suites;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("Testes de persistencia")
@SelectPackages("br.edu.ifsp.integracao")
@IncludeTags("PersistenceTest")
public class SuitePersistencia {
}
