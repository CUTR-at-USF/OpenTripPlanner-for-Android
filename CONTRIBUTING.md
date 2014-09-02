# Contribute to OpenTripPlanner for Android

This guide details how to use issues and pull requests (for new code) to improve OpenTripPlanner for Android.

## Individual Contributor License Agreement (ICLA)

To ensure that the app source code remains fully open-source under a common license, we require that contributors sign [an electronic ICLA](https://docs.google.com/forms/d/1gYT1gLn8TPu-oMYCfWlixm1cG8BU4eCUcSsVDqp3j5o/viewform) before contributions can be merged.

## Code Style and Template

We use the [Android Open-Source Project (AOSP)](http://source.android.com/source/code-style.html) Code Style Guidelines.

We strongly suggest that you use the `AndroidStyle.xml` template file, included in this repository, in Android Studio to format your code:

1. Place `AndroidStyle.xml` in your Android Studio `/codestyles` directory (e.g., `C:\Users\barbeau\.AndroidStudioPreview\config\codestyles`)
2. Restart Android Studio.
3. Go to "File->Settings->Code Style", and under "Scheme" select "AndroidStyle" and click "Ok".
4. Right-click on the files that contain your contributions and select "Reformat Code", check "Optimize imports", and select "Run".

## Maintaining Translations

OTP Android includes translations for several languages, including Spanish, Galacian, German, and Italian.  We rely primarily on contributors
to keep these strings up to date.  As a result,  we should follow the below process when adding new text to the app:

1. **Developer adds new strings** - Add the English representation of the strings to the default `values/strings.xml` file (e.g., `<string name="this_is_a_new_string">This is the new text shown to the user</string>`)
2. **Developer adds template for new strings to alternate language `strings.xml` files** - Add a commented-out placeholder to the `values-xx/strings.xml` files for all translations, so that the translator can easily see what strings haves been added.  For example, to show that the Spanish translation needs to be updated, the `values-es/strings.xml` file should be updated to include `<!-- <string name="this_is_a_new_string">This is the new text shown to the user</string> -->`)
3. **Developer opens an issue on the [issue tracker](https://github.com/CUTR-at-USF/OpenTripPlanner-for-Android/issues) for each translation that needs to be updated** - When approaching a new release, for each language that needs to be updated the developer should open a new issue on the issue tracker, and tag past translation contributors via Github usernames for that language.  For example, if @vreixo contributed Spanish translations in the past, an issue would be opened saying "Need to update Spanish translations", and @vreixo would be tagged in the issue description with a request to update the translations, including a link to the file that needs to be updated.  Find past translation contributors by reviewing the Github history for the given `values-xx/strings.xml` file.  Since contributors are often volunteers, please be respectful of their time and only open issues for completed features after English versions of the text has been finalized.
4. **Translation contributor updates the alternate language `strings.xml` file(s)** - After the translation contributor is tagged on the issue, they can easily see the currently commented out strings that need to be updated in the given `values-xx/strings.xml` file.  They will uncomment this string, and add the appropriate translation.  For example, the commented out XML from step 2 above would be changed to `<string name="this_is_a_new_string">Éste es el nuevo texto que se muestra al usuario</string>`.  The translation contributor would then open a Github pull request (or post them in a comment on the issue, if they aren't familiar with Git) with the new translations for all new strings in the given `values-xx/strings.xml` file. A much easier way to make the pull request process, withouht any knowledge about Git, is to click on `Edit` button in the language file, edit it, write a description of the change and just press on `Propose file change` (the process for Git will be exactly the same, but GitHub will do it for us).

Since there is considerable overhead for the above process, whenever possible please consider using images to convey meaning, instead of text.

## Closing policy for issues and pull requests

OpenTripPlanner for Android is a popular project and the capacity to deal with issues and pull requests is limited. Out of respect for our volunteers, issues and pull requests not in line with the guidelines listed in this document may be closed without notice.

Please treat our volunteers with courtesy and respect, it will go a long way towards getting your issue resolved.

Issues and pull requests should be in English and contain appropriate language for audiences of all ages.

## Issue tracker

The [issue tracker](https://github.com/CUTR-at-USF/OpenTripPlanner-for-Android/issues) is only for obvious bugs, misbehavior, & feature requests in the latest stable or development release of OpenTripPlanner for Android. When submitting an issue please conform to the issue submission guidelines listed below. Not all issues will be addressed and your issue is more likely to be addressed if you submit a pull request which partially or fully addresses the issue.

### Issue tracker guidelines

**[Search](https://github.com/CUTR-at-USF/OpenTripPlanner-for-Android/search?q=&ref=cmdform&type=Issues)** for similar entries before submitting your own, there's a good chance somebody else had the same issue or feature request. Show your support with `:+1:` and/or join the discussion. Please submit issues in the following format (as the first post) and feature requests in a similar format:

1. **Summary:** Summarize your issue in one sentence (what goes wrong, what did you expect to happen)
2. **Steps to reproduce:** How can we reproduce the issue?
3. **Expected behavior:** What did you expect the app to do?
4. **Observed behavior:** What did you see instead?  Describe your issue in detail here.
5. **Device and Android version:** What make and model device (e.g., Samsung Galaxy S3) did you encounter this on?  What Android version (e.g., Android 4.0 Ice Cream Sandwich) are you running?  Is it the stock version from the manufacturer or a custom ROM?
6. **Screenshots:** Can be created by pressing the Volume Down and Power Button at the same time on Android 4.0 and higher.
7. **Possible fixes**: If you can, link to the line of code that might be responsible for the problem.

## Pull requests

We welcome pull requests with fixes and improvements to OpenTripPlanner for Android code, tests, and/or documentation. The features we would really like a pull request for are [open issues with the enhancements label](https://github.com/CUTR-at-USF/OpenTripPlanner-for-Android/issues?labels=enhancement&page=1&state=open).

### Pull request guidelines

If you can, please submit a pull request with the fix or improvements including tests. If you don't know how to fix the issue but can write a test that exposes the issue we will accept that as well. In general bug fixes that include a regression test are merged quickly while new features without proper tests are least likely to receive timely feedback. The workflow to make a pull request is as follows:

1. Fork the project on GitHub
2. Create a feature branch
3. Write tests and code
4. If your code includes any new text shown to the user, update the alternate language `values-xx/strings.xml` files with commented out templates as outlined in the "Maintaining Translations" section above.
5. Apply the `AndroidStyle.xml` style template to your code in Android Studio.
6. If you have multiple commits please combine them into one commit by [squashing them](http://git-scm.com/book/en/Git-Tools-Rewriting-History#Squashing-Commits)
7. Push the commit to your fork
8. Submit a pull request with a motive for your change and the method you used to achieve it
9. [Search for issues](https://github.com/CUTR-at-USF/OpenTripPlanner-for-Android/search?q=&ref=cmdform&type=Issues) related to your pull request and mention them in the pull request description or comments

We will accept pull requests if:

* The code has proper tests and all tests pass (or it is a test exposing a failure in existing code)
* It can be merged without problems (if not please use: `git rebase master`)
* It doesn't break any existing functionality
* It's quality code that conforms to standard style guides and best practices
* The description includes a motive for your change and the method you used to achieve it
* It is not a catch all pull request but rather fixes a specific issue or implements a specific feature
* It keeps the OpenTripPlanner for Android code base clean and well structured
* We think other users will benefit from the same functionality
* If it makes changes to the UI the pull request should include screenshots
* It is a single commit (please use `git rebase -i` to squash commits)

## License

By contributing code to this project via pull requests, patches, or any other process, you are agreeing to license your contributions under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
