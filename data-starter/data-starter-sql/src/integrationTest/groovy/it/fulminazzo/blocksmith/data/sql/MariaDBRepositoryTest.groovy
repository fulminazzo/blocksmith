package it.fulminazzo.blocksmith.data.sql

import it.fulminazzo.blocksmith.data.sql.helper.MariaDBIntegrationTestHelper
import it.fulminazzo.blocksmith.data.sql.helper.SqlIntegrationTestHelper

class MariaDBRepositoryTest extends SqlRepositoryTest {

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
        return new MariaDBIntegrationTestHelper()
    }

}
