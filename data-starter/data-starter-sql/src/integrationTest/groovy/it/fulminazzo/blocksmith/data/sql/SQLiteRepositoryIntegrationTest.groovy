package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.SQLiteIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class SQLiteRepositoryIntegrationTest extends SqlRepositoryIntegrationTest {

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
        return new SQLiteIntegrationTestHelper()
    }

}
