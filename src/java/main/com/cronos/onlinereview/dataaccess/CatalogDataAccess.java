/*
 * Copyright (C) 2010 - 2013 TopCoder Inc., All Rights Reserved.
 */
package com.cronos.onlinereview.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.topcoder.dde.catalog.ComponentVersionInfo;
import com.topcoder.dde.catalog.Document;
import com.topcoder.naming.jndiutility.JNDIUtils;
import com.topcoder.shared.dataAccess.resultSet.ResultSetContainer;
import com.topcoder.util.config.ConfigManagerException;
import com.topcoder.util.idgenerator.IdGenerator;
import com.topcoder.util.idgenerator.sql.DB;

/**
 * <p>A simple DAO for component catalog backed up by Query Tool.</p>
 *
 * @author TCSASSEMBLER
 * @version 2.0
 */
public class CatalogDataAccess extends BaseDataAccess {

    /**
     * <p>A <code>DB</code> providing the database for ID generation.</p>
     */
    private DB idGeneratorDB;

    /**
     * <p>A <code>Strng</code> providing the name of the table keeping the details for ID generation.</p>
     */
    private String idGeneratorTableName;

    /**
     * <p>A <code>String</code> providing the name for column in ID generation table providing the names of ID
     * generation sequences.</p>
     */
    private String idGeneratorUserDefColumnName;

    /**
     * <p>A <code>String</code> providing the name for column in ID generation table providing the current highest
     * generated ID.</p>
     */
    private String idGeneratorHighValueColumnName;

    /**
     * <p>A <code>long</code> providing the maximum value for IDs to be generated by ID generator.</p>
     */
    private long idGeneratorMaxHighValue;

    /**
     * <p>A <code>int</code> providing the maximum value for lowest value for IDs to be generated by ID generator.</p>
     */
    private int idGeneratorMaxLowValue;

    /**
     * <p>A <code>boolean</code> providing the flag indicating whether ID generator is to be auto-initialized or
     * not.</p>
     */
    private boolean idGeneratorAutoInit;

    /**
     * <p>A <code>String</code> providing the name in JNDI context for data source for ID generator.</p>
     */
    private String idGeneratorDataSourceName;

    /**
     * <p>Constructs new <code>CatalogDataAccess</code> instance. This implementation does nothing.</p>
     */
    public CatalogDataAccess() {
    }

    /**
     * <p>Sets the name in JNDI context for data source for ID generator.</p>
     *
     * @param idGeneratorDataSourceName a <code>String</code> providing the name in JNDI context for data source for ID
     *                                  generator.
     */
    public void setIdGeneratorDataSourceName(String idGeneratorDataSourceName) {
        this.idGeneratorDataSourceName = idGeneratorDataSourceName;
    }

    /**
     * <p>Sets the name of the table keeping the details for ID generation.</p>
     *
     * @param idGeneratorTableName a <code>String</code> providing the name of the table keeping the details for ID
     *        generation.
     */
    public void setIdGeneratorTableName(String idGeneratorTableName) {
        this.idGeneratorTableName = idGeneratorTableName;
    }

    /**
     * <p>Sets the database for ID generation.</p>
     *
     * @param idGeneratorDB a <code>DB</code> providing the database for ID generation.
     */
    public void setIdGeneratorDB(DB idGeneratorDB) {
        this.idGeneratorDB = idGeneratorDB;
    }

    /**
     * <p>Sets the name for column in ID generation table providing the current highest generated ID.</p>
     *
     * @param idGeneratorHighValueColumnName a <code>String</code> providing the name for column in ID generation table
     *        providing the current highest generated ID.
     */
    public void setIdGeneratorHighValueColumnName(String idGeneratorHighValueColumnName) {
        this.idGeneratorHighValueColumnName = idGeneratorHighValueColumnName;
    }

    /**
     * <p>Sets the name for table in ID generation table providing the names of ID generation sequences.</p>
     *
     * @param idGeneratorUserDefColumnName a <code>String</code> providing the name for table in ID generation table
     *        providing the names of ID generation sequences.
     */
    public void setIdGeneratorUserDefColumnName(String idGeneratorUserDefColumnName) {
        this.idGeneratorUserDefColumnName = idGeneratorUserDefColumnName;
    }

    /**
     * <p>Sets the maximum value for lowest value for IDs to be generated by ID generator.</p>
     *
     * @param idGeneratorMaxLowValue a <code>int</code> providing the maximum value for lowest value for IDs to be
     *        generated by ID generator.
     */
    public void setIdGeneratorMaxLowValue(int idGeneratorMaxLowValue) {
        this.idGeneratorMaxLowValue = idGeneratorMaxLowValue;
    }

    /**
     * <p>Sets the maximum value for IDs to be generated by ID generator.</p>
     *
     * @param idGeneratorMaxHighValue a <code>long</code> providing the maximum value for IDs to be generated by ID
     *        generator.
     */
    public void setIdGeneratorMaxHighValue(long idGeneratorMaxHighValue) {
        this.idGeneratorMaxHighValue = idGeneratorMaxHighValue;
    }

    /**
     * <p>Sets the flag indicating whether ID generator is to be auto-initialized or not.</p>
     *
     * @param idGeneratorAutoInit a <code>boolean</code> providing the flag indicating whether ID generator is to be
     *                            auto-initialized or not.
     */
    public void setIdGeneratorAutoInit(boolean idGeneratorAutoInit) {
        this.idGeneratorAutoInit = idGeneratorAutoInit;
    }

    /**
     * <p>Gets the details for component version for specified component ID and version number.</p>
     *
     * <p>As of current version the returned {@link ComponentVersionInfo} object has only <code>versionId</code>
     * property set. All other properties are not initialized.</p>
     *
     * @param componentId a <code>long</code> providing the component ID.
     * @param versionNumber a <code>long</code> providing the component version number.
     * @return a <code>ComponentVersionInfo</code> providing the details for component version.
     */
    public ComponentVersionInfo getComponentVersionInfo(long componentId, long versionNumber) {
        final String queryName = "comp_version";
        String[] queryArgNames = new String[] {"cd", "vid"};
        String[] queryArgs = new String[] {String.valueOf(componentId), String.valueOf(versionNumber)};

        Map<String, ResultSetContainer> results = runQuery(queryName, queryArgNames, queryArgs);
        ResultSetContainer versionData = results.get(queryName);

        if (versionData.isEmpty()) {
            return null;
        } else {
            long versionId = versionData.getLongItem(0, "version_id");
            return new ComponentVersionInfo(versionId, 0, null, null, 0, new Date(), 0, false);
        }
    }

    /**
     * <p>Gets the documents for specified component version.</p>
     *
     * @param componentVersionId a <code>long</code> providing the component version ID.
     * @return a <code>Collection</code> listing the documents for specified component version. If there are no such
     *         documents then empty collection is returned.
     */
    public Collection<Document> getDocuments(long componentVersionId) {
        final String queryName = "comp_version_documents";
        Map<String, ResultSetContainer> results = runQuery(queryName, "cv", String.valueOf(componentVersionId));
        ResultSetContainer data = results.get(queryName);
        Collection<Document> documents = new ArrayList<Document>();
        for (int i = 0 ; i < data.size(); i++) {
            Document document = new Document(data.getLongItem(i, "document_id"),
                                             data.getStringItem(i, "document_name"),
                                             data.getStringItem(i, "url"),
                                             data.getLongItem(i, "document_type_id"));
            documents.add(document);
        }
        return documents;
    }

    /**
     * <p>Adds new document for specified component version.</p>
     *
     * @param componentVersionId a <code>long</code> providing the component version ID.
     * @param document a <code>Document</code> providing the details for new document.
     * @return a new <code>Document</code> instance providing the details for added document.
     * @throws DataAccessException if an unexpected error occurs.
     */
    public Document addDocument(long componentVersionId, Document document) {
        Connection connection = getTCSCatalogDBConnection();
        PreparedStatement ps = null;
        try {
            long documentId = generateNextCatalogScopedId();
            ps = connection.prepareStatement
                ("INSERT INTO comp_documentation (document_id, comp_vers_id, document_type_id, document_name, url) "
                 + "VALUES (?, ?, ?, ?, ?)");
            ps.setLong(1, documentId);
            ps.setLong(2, componentVersionId);
            ps.setLong(3, document.getType());
            ps.setString(4, document.getName());
            ps.setString(5, document.getURL());

            int rowsInserted = ps.executeUpdate();
            if (rowsInserted != 1) {
                throw new DataAccessException("Failed to insert record for new document. Number of records inserted: "
                                              + rowsInserted);
            }
            return new Document(documentId, document.getName(), document.getURL(), document.getType());
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add new component version document", e);
        } finally {
            close(ps);
            close(connection);
        }
    }

    /**
     * <p>Updates existing document for specified component version.</p>
     *
     * @param componentVersionId a <code>long</code> providing the component version ID.
     * @param document a <code>Document</code> providing the details for document.
     * @throws DataAccessException if an unexpected error occurs.
     */
    public void updateDocument(long componentVersionId, Document document) {
        Connection connection = getTCSCatalogDBConnection();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(
                "UPDATE comp_documentation SET document_type_id = ?, document_name = ?, url = ? "
                + " WHERE document_id = ? AND comp_vers_id = ?");
            ps.setLong(1, document.getType());
            ps.setString(2, document.getName());
            ps.setString(3, document.getURL());
            ps.setLong(4, document.getId());
            ps.setLong(5, componentVersionId);

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
                throw new DataAccessException(
                    "Failed to update record for existing document. Number of records updated: " + rowsUpdated);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update existing component version document", e);
        } finally {
            close(ps);
            close(connection);
        }
    }

    /**
     * <p>Generates new ID using ID Generator used by the <code>Component Catalog</code> system.</p>
     *
     * @return a <code>long</code> providing next generated unique ID.
     */
    protected long generateNextCatalogScopedId() {
        try {
            if (!IdGenerator.isInitialized()) {
                Context context = JNDIUtils.getContext("default");
                DataSource dataSource = (DataSource) context.lookup(this.idGeneratorDataSourceName);
                IdGenerator.init(this.idGeneratorDB, dataSource, this.idGeneratorTableName,
                                 this.idGeneratorUserDefColumnName, this.idGeneratorHighValueColumnName,
                                 this.idGeneratorMaxHighValue, this.idGeneratorMaxLowValue, this.idGeneratorAutoInit);
            }
            return IdGenerator.nextId();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to generate next catalog scoped ID", e);
        } catch (NamingException e) {
            throw new DataAccessException("Failed to locate datasource " + this.idGeneratorDataSourceName
                                          + "for ID generator", e);
        } catch (ConfigManagerException e) {
            throw new DataAccessException("Failed to locate datasource " + this.idGeneratorDataSourceName
                                          + "for ID generator", e);
        }
    }
}
