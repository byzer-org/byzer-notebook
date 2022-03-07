mvn clean install test && \
echo "================================================================"$SONAR_TOKEN && \
  export SONAR_TOKEN="$1" && echo "================================================================"$SONAR_TOKEN && \
  export target_dir="/usr/lib/jvm" && \
  export JAVA_HOME="$target_dir/jdk11" && \
  wget --no-check-certificate --no-verbose "https://repo.huaweicloud.com/java/jdk/11.0.2+9/jdk-11.0.2_linux-x64_bin.tar.gz" \
  --directory-prefix "$target_dir" && \
  tar -xf "$target_dir/jdk-11.0.2_linux-x64_bin.tar.gz" -C "$target_dir" && \
  mv ${target_dir}/jdk-11.0.2 $JAVA_HOME && \
  rm -f ${target_dir}/jdk-11.0.2_linux-x64_bin.tar.gz && \
  mvn -DskipTests verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -Dsonar.projectKey=byzer-org_byzer-notebook