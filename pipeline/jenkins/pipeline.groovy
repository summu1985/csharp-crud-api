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
                            timeout(7) { 
                                openshift.selector("dc", "csharp-crud-api-git").related('pods').untilEach(1) {
                                    return (it.object().status.phase == "Running")
                                }
                            }
                        }
                    }
                }
            }
        }
        stage('unit-test') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject('indigo-hackathon') {
                            echo "Using project: ${openshift.project()}"
                            final String url = ""
                            def connected = openshift.verifyService("csharp-crud-api-git")
                            if (connected) {
                                /*httpRequest url:"https://csharp-crud-api-git-indigo-hackathon.apps.cluster-m227s.m227s.sandbox1272.opentlc.com/api/users", 
							        validResponseCodes:'200'*/
							    echo "Connected to csharp-crud-api-git service"
                            }
                            else {
                                echo "Unable to connect to csharp-crud-api-git service"
                            }
                        }
                    }
                }
            }
        }
        stage('tag-uat-build') {
            steps {
                script {
                    openshift.withCluster() {
                        openshift.withProject('indigo-hackathon') {
                            openshift.tag("csharp-crud-api-git:latest", "csharp-crud-api-git-staging:latest")
                        }
                    }
                }
            }
        }
    }
}