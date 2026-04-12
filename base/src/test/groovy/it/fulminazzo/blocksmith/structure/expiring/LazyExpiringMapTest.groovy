package it.fulminazzo.blocksmith.structure.expiring

class LazyExpiringMapTest extends ExpiringMapImplTest {

    @Override
    protected ExpiringMap<String, String> createMap() {
        return new LazyExpiringMap<>()
    }

}
