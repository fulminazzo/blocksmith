package it.fulminazzo.blocksmith.data.memory;

import it.fulminazzo.blocksmith.data.CacheRepositorySettings;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, doNotUseGetters = true)
@ToString(callSuper = true, doNotUseGetters = true)
public final class MemoryRepositorySettings extends CacheRepositorySettings<MemoryRepositorySettings> {

}
