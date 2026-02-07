import org.secops.StateStore

def init(Map config) {

    def state = [
        assessmentId: UUID.randomUUID().toString(),
        project      : config.project,
        repo         : config.repo,
        commit       : env.GIT_COMMIT,
        target       : config.target,
        stages       : [:],
        createdAt    : new Date().toString()
    ]

    StateStore.save(this, state)
    return state.assessmentId
}
