package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.H2IntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class H2SqlRepositoryIntegrationTest extends SqlRepositoryIntegrationTest {

    void setupSpec() {
        setupSuite()
    }

    void cleanupSpec() {
        cleanupSuite()
    }

    void setup() {
        setupSingle()
    }

    void cleanup() {
        cleanupSingle()
    }

    @Override
    protected SqlIntegrationTestHelper newTestHelper() {
        return new H2IntegrationTestHelper()
    }

}
