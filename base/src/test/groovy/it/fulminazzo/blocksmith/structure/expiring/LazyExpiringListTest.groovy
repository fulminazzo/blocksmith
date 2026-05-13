package it.fulminazzo.blocksmith.structure.expiring

class LazyExpiringListTest extends ExpiringListImplTest {

    @Override
    protected ExpiringList<String> createList() {
        return new LazyExpiringList<>()
    }

}
