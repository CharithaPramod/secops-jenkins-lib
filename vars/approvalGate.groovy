def call(Map args = [:]) {

    def gateName = args.gateName ?: 'UNNAMED_GATE'
    def requiredRole = args.requiredRole ?: 'UNDEFINED_ROLE'

    timeout(time: 2, unit: 'DAYS') {

        def decision = input(
            id: "${gateName}_APPROVAL",
            message: "Approval Required: ${gateName}\nRequired Role: ${requiredRole}",
            ok: "Submit",
            parameters: [
                choice(
                    name: 'DECISION',
                    choices: ['APPROVE', 'REJECT'],
                    description: 'Select decision'
                ),
                text(
                    name: 'COMMENTS',
                    description: 'Mandatory justification'
                )
            ]
        )

        echo "Approval decision for ${gateName}: ${decision.DECISION}"
        echo "Comments: ${decision.COMMENTS}"

        if (decision.DECISION == 'REJECT') {
            error("Pipeline stopped at approval gate: ${gateName}")
        }
    }
}
