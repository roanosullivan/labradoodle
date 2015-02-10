# Design Notes on Consuming the Gitlab API


## Docs

 * [Google Groups > Gitlab HQ](https://groups.google.com/forum/#!forum/gitlabhq)
 * [Gitlab API Docs](http://doc.gitlab.com/ce/api/README.html)
    * [/issues](http://doc.gitlab.com/ce/api/issues.html)

## Libs and SDKs

 * [Java Gitlab API](https://github.com/timols/java-gitlab-api)
     * Docs are anemic; see the [GitlabAPIT](https://github.com/timols/java-gitlab-api/blob/master/src/test/java/org/gitlab/api/GitlabAPIT.java) for usage examples. Otherwise, [GitlabAPI.java](https://github.com/timols/java-gitlab-api/blob/master/src/main/java/org/gitlab/api/GitlabAPI.java) is easy to read.
 * [CLI API Clients](https://about.gitlab.com/applications/#api-clients)
     * [CLI Client and Shell](http://narkoz.github.io/gitlab/) and Ruby Wrapper ([actively developed](https://github.com/NARKOZ/gitlab/commits/master)!)
     * Alt [CLI Client](https://github.com/numa08/git-gitlab) ([10 months stale](https://github.com/numa08/git-gitlab/commits/master))


## Endpoints

 * [Projects]($HOST/api/v3/projects?private_token=$PRIVATE_TOKEN)
 * [Issues for Project 1]($HOST/api/v3/project/1/issues?private_token=$PRIVATE_TOKEN)
 * [My Issues]($HOST/api/v3/issues?private_token=$PRIVATE_TOKEN)
