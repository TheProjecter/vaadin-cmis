Alfresco-Vaadin integration (com.vaadin.data.Container, Item, Property, plus helpful friends) over CMIS.

1) CRUD, search, render arbitrarily complex content schemas using standard Vaadin UI components.

2) Designer-editable visual form "templates" that can be bound at runtime to live CMIS data.

3) Lots of fill-in-the-gap tools and utilities that help keep the schema proliferation to a minimum. For example:
  * Define `<mandatory-aspects`> in your alfresco data model and those aspect properties will be available to any UI component that holds the related type.
  * `PropertyUniqueValidator` to do runtime checks of user data entry for uniqueness, format, etc
  * AlfrescoCmisSessionDataSource makes Spring configuration of CMIS repository access trivial.

To get started: GettingStarted.

Coming soon:

1) SSO with Shiro, CAS and Alfresco (to achieve user-bound SSO for CMIS calls to the repository, and allow Alfresco to process permissions based on CAS login).

2) A runtime for importing Excel documents and turning them into complex CMIS content trees, and a configuration language for driving it.

3) More CMIS UI goodies - datalist manager, login screen, etc.

We're building this stuff because it's an awesome way to solve the problems we set out to address, and we're sharing it because we want to see it grow. Your feedback is welcome and requested.

NOTE: This is all alpha code. It works, some of it is being used in production, but it is immature and you need to know what you're doing to use it (for now).