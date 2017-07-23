LastPass Java API

This is unofficial LastPass API.

This is a port of [C# API](https://github.com/detunized/lastpass-sharp).

This library implements fetching and parsing of LastPass data. The library is still in the proof of concept stage and doesn't support all LastPass features yet. Only account information (logins, passwords, urls, etc.) is available so far.

There is a low level API which is used to fetch the data from the LastPass server and parse it. Normally this is not the one you would want to use. What you want is the Vault class which hides all the complexity and exposes all the accounts already parsed, decrypted and ready to use. See the example program for detail.

A quick example of accessing your account information:

using LastPass;

Vault vault = Vault.Create(username, password);
for (Account i : vault.getAccounts())
    System.out.println("%s: %s, %s", i.getName(), i.getUsername(), i.getPassword());

The blob received from LastPass could be safely stored locally (it's well encrypted) and reused later on.
Contributing

Contribution in any form and shape is very welcome. Have comments, suggestions, patches, pull requests? All of the above are welcome.
License

The library is released under the MIT license.
