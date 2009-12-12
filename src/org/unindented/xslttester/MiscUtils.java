package org.unindented.xslttester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

/**
 * Misc utils for working with XML and XSLT.
 */
public class MiscUtils
{
    public static String prettify(final String xml)
    {
        StringWriter output = new StringWriter();

        try
        {
            OutputFormat format = OutputFormat.createPrettyPrint();
            XMLWriter writer = new XMLWriter(output, format);

            Document document = DocumentHelper.parseText(xml);
            writer.write(document);
            writer.flush();
        }
        catch (Exception e)
        {
            return xml;
        }

        return output.toString();
    }

    public static String transform(final Source xml, final Source xslt, final Map params, final Writer err)
        throws TransformerException
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setErrorListener(new ErrorListener()
        {
            public void warning(final TransformerException e) throws TransformerException
            {
                append(e.getMessageAndLocation());
            }

            public void error(final TransformerException e) throws TransformerException
            {
                append(e.getMessageAndLocation());
            }

            public void fatalError(final TransformerException e) throws TransformerException
            {
                append(e.getMessageAndLocation());
            }

            private void append(final String msg)
            {
                if (err != null)
                {
                    try
                    {
                        err.append(msg + "\n");
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        });

        Transformer transform = factory.newTransformer(xslt);

        if (params != null)
        {
            for (Iterator iter = params.entrySet().iterator(); iter.hasNext();)
            {
                Entry param = (Entry) iter.next();
                transform.setParameter((String) param.getKey(), param.getValue());
            }
        }

        StreamResult result = new StreamResult(new StringWriter());
        transform.transform(xml, result);

        return result.getWriter().toString();
    }

    public static String convertStreamToString(final InputStream is)
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();

        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return sb.toString();
    }
}
