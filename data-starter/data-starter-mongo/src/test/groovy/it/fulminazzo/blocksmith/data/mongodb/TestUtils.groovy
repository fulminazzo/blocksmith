package it.fulminazzo.blocksmith.data.mongodb

import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.packageresolver.PlatformPackageResolver
import de.flapdoodle.embed.mongo.transitions.Mongod
import de.flapdoodle.embed.mongo.transitions.PackageOfCommandDistribution
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess
import de.flapdoodle.embed.process.distribution.Distribution
import de.flapdoodle.embed.process.distribution.PackageResolver
import de.flapdoodle.os.CommonOS
import de.flapdoodle.os.ImmutablePlatform
import de.flapdoodle.os.linux.LinuxDistribution
import de.flapdoodle.os.linux.UbuntuVersion
import de.flapdoodle.reverse.TransitionWalker
import de.flapdoodle.reverse.transitions.Start
import org.jetbrains.annotations.NotNull

final class TestUtils {

    static @NotNull TransitionWalker.ReachedState<RunningMongodProcess> startServer(final int port) {
         return Mongod.builder()
                 .net(Start.to(Net).initializedWith(Net.of('localhost', port, de.flapdoodle.net.Net.localhostIsIPv6())))
                 .packageOfDistribution(PackageOfCommandDistribution.builder()
                         .commandPackageResolver(command -> {
                             def resolver = new PlatformPackageResolver(command)
                             return (PackageResolver) ((distribution) -> {
                                 try {
                                     return resolver.packageFor(distribution)
                                 } catch (IllegalArgumentException e) {
                                     if (distribution.platform().operatingSystem() == CommonOS.Linux) {
                                         def fallbackDist = Distribution.of(
                                                 distribution.version(),
                                                 ImmutablePlatform.copyOf(distribution.platform())
                                                         .withDistribution(LinuxDistribution.Ubuntu)
                                                         .withVersion(UbuntuVersion.Ubuntu_22_04)
                                         )
                                         return resolver.packageFor(fallbackDist)
                                     }
                                     throw e
                                 }
                             })
                         })
                         .build()
                 )
                 .build()
                 .start(Version.Main.V7_0)
    }

}
