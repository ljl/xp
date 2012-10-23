<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <title>Enonic WEM - Admin Console</title>
  <style type="text/css">
    body {
      font: 85% Arial;
      padding: 0 .2em;
    }

    hr {
      height: 1px;
      color: #ccc;
      border: none;
      background: #ccc;
    }

    #github-commits {
      background-color: #fff;
    }

    #github-commits ul {
      margin: 0;
      padding: 0;
      list-style-type: none;
    }

    #github-commits ul li {
      margin-bottom: .15em;
    }

    #github-commits img.github-avatar {
      vertical-align: top;
    }
  </style>

  <!-- ExtJS -->
  <link rel="stylesheet" type="text/css" href="resources/lib/ext/resources/css/ext-all-scoped.css">
  <script>
    Ext = {
      buildSettings: {
        'scopeResetCSS': true
      }
    };
  </script>
  <script type="text/javascript" src="resources/lib/ext/ext-all.js"></script>

  <script type="text/javascript" src="../dev/live-edit/app/lib/jquery-1.8.0.min.js"></script>
  <script type="text/javascript" src="resources/lib/jquery/github.commits.widget-min.js"></script>
</head>
<body>
<h1>Enonic WEM - Admin Console</h1>

<p id="search-container">

</p>

<h2>Tools</h2>
<ul class="links">
  <li>
    <a href="../tests/">Tests</a>
  </li>
  <li>
    <a href="http://versiontest/5-0/docs/">Docs</a> (Move to leela?)
  </li>
</ul>

<hr/>

<h2>Apps</h2>
<ul class="links">
  <li>
    <a href="main.jsp">Main</a>
  </li>
  <li>
    <a href="app-content-manager.jsp">Content Manager</a>
  </li>
  <li>
    <a href="app-dashboard.jsp">Dashboard</a>
  </li>
  <li>
    <a href="app-account.jsp">Accounts</a>
  </li>
  <li>
    <a href="app-system.jsp">System</a>
    <ul>
      <li><a href="about.jsp">About</a></li>
      <li><a href="app-property.jsp">Properties</a></li>
      <li><a href="app-system-cache.jsp">Cache</a></li>
      <li><a href="app-userstore.jsp">Userstore</a></li>
      <li><a href="app-language.jsp">Languages</a></li>
    </ul>
  </li>
  <li>
    <a href="app-data-designer.jsp">Data Designer</a>
  </li>
</ul>

<hr/>

<h2>R&amp;D</h2>
<ul class="links">
  <li><a href="../dev/live-edit/page/page.jsp">Live Edit: page component is &lt;body&gt;</a></li>
  <li><a href="../dev/live-edit/page/page2.jsp">Live Edit: page component is a descendant of &lt;body&gt;</a></li>
  <li><a href="../dev/live-edit/page/plug-ins.jsp">Live Edit: with draggables containing plugins</a></li>
  <li><a href="../dev/live-edit/page/blank.jsp">Live Edit: Blank page</a></li>
  <li>
    <a href="../dev/filter/index.html">Facet checkboxes with Ext Data View</a>
  </li>
  <li>
    <a href="../dev/examples/summary.html">Summary</a>
  </li>
  <li>
    <a href="../dev/examples/i18n.html">i18n</a>
  </li>
  <li>
    <a href="../dev/html-templates/user-preview.html">User Preview</a>
  </li>
  <li>
    <a href="../dev/examples/file-upload-test.html">File Upload</a>
  </li>
  <li>
    <a href="../dev/html-templates/wizard-navigation.html">Wizard Navigation</a>
  </li>
  <li>
    <a href="../dev/html-templates/wizard-navigation2.html">Wizard Navigation (rev 2)</a>
  </li>
  <li>
    <a href="../dev/html-templates/tilebased-start-menu.html">Tile Based Start Menu</a>
  </li>
  <li>
    <a href="../dev/examples/topbar.html">Top Bar</a>
  </li>
</ul>

<hr/>
<h2>CVS (Git)</h2>

<p>
  Repo: <a href="http://github.com/enonic/wem-ce">enonic/wem-ce</a>
</p>

<div id="github-commits"></div>

<script>
  $(function () {
    $('#github-commits').githubInfoWidget(
        { user: 'enonic', repo: 'wem-ce', branch: 'master', last: 10, limitMessageTo: 50 });
  });
</script>

<script>
  Ext.onReady(function () {

    Ext.define('PageLinks', {
      extend: 'Ext.data.Model',
      fields: [
        {name: 'text', type: 'string'},
        {name: 'url', type: 'string'}
      ]
    });

    var pageLinksStore = Ext.create('Ext.data.Store', {
      model: 'PageLinks',
      data: []
    });

    function addLinks() {
      var domQuery = Ext.dom.Query,
          links = domQuery.select('ul.links li'),
          aElement, text, href;

      Ext.Array.each(links, function (li, index, all) {
        aElement = Ext.fly(domQuery.selectNode('a', li));
        pageLinksStore.add({text: aElement.getHTML(), url: aElement.getAttribute('href')});
      });
    }

    addLinks();

    var commandLinePanel = Ext.create('Ext.panel.Panel', {
      renderTo: 'search-container',
      frame: true,
      // title: 'Command Line',
      width: '100%',
      padding: 10,
      layout: 'anchor',
      items: [
        {
          xtype: 'component',
          style: 'margin-bottom:10px',
          html: 'Type any app name and press enter'
        },
        {
          xtype: 'combo',
          store: pageLinksStore,
          displayField: 'text',
          valueField: 'url',
          queryMode: 'local',
          typeAhead: true,
          hideLabel: true,
          hideTrigger: true,
          anchor: '100%',
          minChars: 1,
          listeners: {
            select: function (combo, selection) {
              var link = selection[0];
              if (link) {
                window.location = link.get('url');
              }
            },
            render: function (combo) {
              combo.focus();
            }
          }
        }
      ]
    });

  });
</script>


</body>
</html>


