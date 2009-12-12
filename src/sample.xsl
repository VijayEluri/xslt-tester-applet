<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="/">
    <html>
      <body>
        <h1>Greatest Albums of All Time</h1>
        <table>
          <tr>
            <th>Title</th>
            <th>Artist</th>
          </tr>
          <xsl:for-each select="albums/album">
            <tr>
              <td>
                <xsl:value-of select="title" />
              </td>
              <td>
                <xsl:value-of select="artist" />
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
