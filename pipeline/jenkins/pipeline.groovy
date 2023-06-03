pipeline {
    agent any
    stages {
        stage ('build') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject('indigo-hackathon') {
                            echo "Using project: ${openshift.project()}"
  
                            def buildConfigExists = openshift.selector("bc", "csharp-crud-api-git").exists() 
    
                            openshift.selector("bc", "csharp-crud-api-git").startBuild("--follow")
                        }
                    }
                }
            }
        }
        stage ('deploy') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject('indigo-hackathon') {
                            echo "Using project: ${openshift.project()}"
                            def dc = openshift.selector("dc", "csharp-crud-api-git").rollout().latest()
                            timeout(5) { 
                                openshift.selector("dc", "csharp-crud-api-git").related('pods').untilEach(1) {
                                    return (it.object().status.phase == "Running")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
