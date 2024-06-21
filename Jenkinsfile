def WEBAPP_DIR = '${WORKSPACE}/panchayatseva-webapp'
def DEVOPS_DIR = '${WORKSPACE}/panchayatseva-devops'
    
def remote = [:]
    remote.name = 'devopsadmin'
    remote.host = '172.21.3.223'
    remote.user = 'devopsadmin'
    remote.identityFile = '/data1/guest-keys/ps-devbox-ein0011/ps-devbox-ein0011'
    remote.port = 59222
    remote.allowAnyHosts = true

pipeline {
    agent none
    environment {
        M2_HOME="/opt/maven"
        MAVEN_HOME="/opt/maven"
        MAVEN="/opt/maven/bin/mvn"
        JAVA_HOME="/opt/jdk11"
        JAVA="/opt/jdk11/bin/java"
        GITHUB_TOKEN = credentials('devops-permanent-token-with-read-access')
        MYSQL_CREDENTIALS=credentials('mysql-credentials-ps')
		CQL_CREDENTIALS=credentials('cql-credentials-ps')
    }
    stages {
        stage('Webapp Sourcecode Checkout') {
            agent {
                node {
                    label 'master'
                }
            }
            steps {
                script {
                    def res = sh(script: "test -d ${WEBAPP_DIR} && echo '1' || echo '0'", returnStdout: true).trim()
                    if (res == '0') {
                        sh "git clone -b dev-tiru --single-branch https://devops-sayukth:$GITHUB_TOKEN@github.com/PanchayatSeva-SB/panchayatseva-webapp.git ${WEBAPP_DIR}"
                    }
                    def res_devops = sh(script: "test -d ${DEVOPS_DIR} && echo '1' || echo '0'", returnStdout: true).trim()
                    if (res_devops == '0'){
                        sh "git clone https://devops-sayukth:$GITHUB_TOKEN@github.com/PanchayatSeva-SB/panchayatseva-devops.git ${DEVOPS_DIR}"
                    }
                    else {
                        def changes_in_webapp = sh(script: "cd ${WEBAPP_DIR} && git fetch && git rev-list HEAD..origin/main --count", returnStdout: true).trim()
                        def changes_in_devops = sh(script: "cd ${DEVOPS_DIR} && git fetch && git rev-list HEAD..origin/main --count", returnStdout: true).trim()
                        if (changes_in_webapp.toInteger() > 0 || changes_in_devops.toInteger() > 0) {
                            echo 'Changes detected. Continue with the build.'
                            sh "git -C ${WEBAPP_DIR} pull"
                            sh "git -C ${DEVOPS_DIR} pull"
                        }
                        else {
                            currentBuild.result = 'ABORTED'
                            error 'Pipeline aborted: No new changes in the repository.'
                        }
                    }
                }
            }
        }
        stage('Artifact Install') {
            agent {
                node {
                    label 'master'
                }
            }
            steps {
                sh "cd ${WEBAPP_DIR} && $MAVEN install package -Djansi.tmpdir=/data/jenkins/java-tmp-dir -DskipTests=true -Dtestng.dtd.http=true -Dsonar.skip=true -Dsnyk.skip=true"
            }
        }
        stage('Config Update') {
            agent {
                node {
                    label 'master'
                }
            }
            steps {
                script {
                    sshCommand remote:remote, command:'rm -rf /tmp/dev-tomcat-conf'
                    sshCommand remote:remote, command:'mkdir /tmp/dev-tomcat-conf'
                    sh "cd ${DEVOPS_DIR} && $MAVEN -e clean install -Djansi.tmpdir=/data/jenkins/java-tmp-dir|| echo build dir fail"
                    sh "mkdir ${DEVOPS_DIR}/target/webapp-config || echo dir exists"
                    sh "mkdir ${DEVOPS_DIR}/target/webapp-config/sb || echo dir exists"
                    sh "mkdir ${DEVOPS_DIR}/target/webapp-config/sb/dev || echo dir exists"
                    sh "$JAVA -Djava.io.tmpdir=/data/jenkins/java-tmp-dir -Denable.debug=true -cp ${WEBAPP_DIR}/target/panchayatseva/WEB-INF/classes/:\"${WEBAPP_DIR}/target/panchayatseva/WEB-INF/lib/*\":\"${WEBAPP_DIR}/target/dependency/*\":\"${DEVOPS_DIR}/target/classes/\" com.sayukth.panchayatseva.webapp.conf.gen.dev_vm.WebAppConfigGeneratorDevVm \"${DEVOPS_DIR}/target/webapp-config/sb/dev\" \"${DEVOPS_DIR}/resources/tmpl/\""
                    sshPut remote:remote, from:"panchayatseva-devops/target/webapp-config/sb/dev/", into:'/tmp/dev-tomcat-conf/'
                    sshCommand remote:remote, command:'sudo cp -rf /tmp/dev-tomcat-conf/dev/* /opt/ps/tomcat/conf/'
                }
            }
        }
        stage('Artifact Distribution') {
            agent {
                node {
                    label 'master'
                }
            }
            steps {
                script {
                    //sh "cp -f ${WEBAPP_DIR}/eev-webapp/target/generated-swagger/swagger-ui/swagger.json /opt/httpd-data/www/html/rest-api/"
                    
                    sshCommand remote:remote, command: "rm -rf /tmp/dev-resources-conf || cannot delete dir"
                    sshCommand remote:remote, command: "mkdir /tmp/dev-resources-conf || echo dir exists"
                    sshPut remote: remote, from: "panchayatseva-webapp/target/panchayatseva.war", into: '/tmp/dev-resources-conf'
                    sshPut remote: remote, from: "panchayatseva-webapp/src/main/resources", into: '/tmp/dev-resources-conf'
                }
            }
        }
        stage('Artifact Deploy') {
            agent {
                node {
                    label 'dev-tmpl'
                }
            }
            steps {
                script {
                    sh "sudo /usr/bin/mysql -u$MYSQL_CREDENTIALS_USR -p$MYSQL_CREDENTIALS_PSW psdb_dev < /tmp/dev-resources-conf/resources/db/update/ps_update.sql"
                    sh "sudo /usr/bin/mysql -u$MYSQL_CREDENTIALS_USR -p$MYSQL_CREDENTIALS_PSW psdb_dev < /tmp/dev-tomcat-conf/dev/user-passwd-update.sql"
                    sh "sudo /usr/bin/cqlsh -kps_blob_dev -u$CQL_CREDENTIALS -p$CQL_CREDENTIALS_PSW 172.21.3.223 9042 -f /tmp/dev-resources-conf/resources/ds/update/ps_update.cql"

                    // sshCommand remote:remote, command:'sudo cp -f /etc/haproxy/haproxy.cfg /etc/haproxy/haproxy-copy.cfg'
                    // sshCommand remote:remote, command: "sudo sed -i \" s|    server $DEV1_DC|#   server $DEV1_DC|g\" /etc/haproxy/haproxy.cfg "
                    // sshCommand remote:remote, command:'sudo systemctl reload haproxy'

                    sh 'sudo pgrep java || true'
                    sh 'sudo systemctl stop tomcat-ps.service || echo tomcat doesnt exist'
                    sh 'sudo rm -rf /opt/ps/tomcat/webapps/ROOT*'
                    sh 'sudo rm -rf /opt/ps/tomcat/logs/*.log'
                    sh 'sudo rm -rf /opt/ps/tomcat/logs/*.out'
                    sh 'sudo cp /tmp/dev-resources-conf/panchayatseva.war /opt/ps/tomcat/webapps/ROOT.war'
                    sh 'sudo chown tomcat:tomcat /opt/ps/tomcat/webapps/ROOT.war'
                    sh 'sudo systemctl start tomcat-ps.service'
                    sh 'sudo pgrep java'
                    sleep(time: 60, unit: 'SECONDS')

                    // sshCommand remote:remote, command:'sudo cp -f /etc/haproxy/haproxy-copy.cfg /etc/haproxy/haproxy.cfg'
                    // sshCommand remote:remote, command:'sudo systemctl reload haproxy'
                }
            }
        }
    }
}
