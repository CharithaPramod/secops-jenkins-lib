import org.secops.StateStore

def call(String stageName, Closure body) {

    def state = StateStore.load(this)

    // Skip if already passed
    if (state.stages[stageName]?.status in ['PASSED', 'RISK_ACCEPTED']) {
        echo "Skipping ${stageName} (already ${state.stages[stageName].status})"
        return
    }

    try {
        body()

        state.stages[stageName] = [
            status: 'PASSED',
            timestamp: new Date().toString()
        ]
        StateStore.save(this, state)

    } catch (Exception e) {

        state.stages[stageName] = [
            status: 'FAILED',
            error: e.message,
            timestamp: new Date().toString()
        ]
        StateStore.save(this, state)
        throw e
    }
}
