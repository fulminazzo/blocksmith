plugins { id("blocksmith.composite-module") }

compositeModule {
    excludedSubmodules = setOf(project.projects.dataStarter.dataStarterMapper.name)
}
