package it.fulminazzo.blocksmith.command

import be.seeseemelk.mockbukkit.MockBukkit
import groovy.util.logging.Slf4j
import it.fulminazzo.blocksmith.BlocksmithApplication
import it.fulminazzo.blocksmith.message.Messenger
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import spock.lang.Specification

@Slf4j
class BukkitCommandRegistryFactoryTest extends Specification {

    void setupSpec() {
        MockBukkit.mock()
    }

    void cleanupSpec() {
        MockBukkit.unmock()
    }

    def 'test that newCommandRegistry returns BukkitCommandRegistry'() {
        when:
        def registry = CommandRegistryFactory.newCommandRegistry(mockApplication())

        then:
        (registry instanceof BukkitCommandRegistry)
    }

    private BlocksmithApplication mockApplication() {
        BlocksmithApplication application = Mock(Plugin, additionalInterfaces: [BlocksmithApplication]) as BlocksmithApplication
        application.messenger >> new Messenger(log)
        application.log >> log
        application.name >> 'blocksmith'
        application.server >> Bukkit.server
        return application
    }

}
