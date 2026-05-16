package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.PostgresSQLIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class PostgresSQLRepositoryIntegrationTest extends SqlRepositoryIntegrationTest {

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
        return new PostgresSQLIntegrationTestHelper()
    }

}
