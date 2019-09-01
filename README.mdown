#Maven plugin for multi module projects

##Goals:
###1) check

If module is not modified then not build him

Before run build will calculating hash sum and put it to 'outputDir' if it equals previous then skip module

######Parameters:
outputDir - directory for hash sum files
nextGoals - mvn goal whom run if module modified
rootArtifact - root artifact whom contains in root pom.xml 



######Run example: 
>mvn -U  -DoutputDir="d:\jp\multimodule-build-maven-plugin" -DnextGoals="compile" -DrootArtifact=stoloto-news888-app finch:multimodule-build-maven-plugin:check