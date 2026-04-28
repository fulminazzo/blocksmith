plugins { id("blocksmith.composite-module") }

compositeModule {
    ignoredSubmodules = setOf(project.projects.dataStarter.dataStarterMapper.name)
}
