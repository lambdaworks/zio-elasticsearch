---
id: about_contributing
title: "Contributing"
---

We welcome contributions from anybody wishing to participate. All code or documentation that is provided must be licensed under [Apache 2.0](https://github.com/lambdaworks/zio-elasticsearch/blob/main/LICENSE).

## General Workflow

1. Make sure you can license your work under Apache 2.0.

2. Before starting work, make sure there is a ticket on the issue, and if not, make sure to create one. It can help accelerate the acceptance process if the change is agreed upon.

3. If you don't have write access to the repository, you should do your work on a local branch of your own fork. If you do have write access to the repository, you should avoid working directly on the main branch.
   
4. When your work is completed, verify it with following commands:

    ```
    sbt check
    sbt +test
    ```

5. Submit a Pull Request.

6. Anyone can comment on a Pull Request, and you are expected to answer questions or to incorporate feedback.

## General Guidelines

- We recommend for the work to be accompanied by unit tests.

- The commit messages should be clear and short, and if more details are needed, specify a body.

- Follow the structure of the code in this repository, and the formatting rules used. You can format your code properly with the following command:

    ```
    sbt prepare
    ```