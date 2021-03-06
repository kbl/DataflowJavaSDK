/*
 * Copyright (C) 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.dataflow.sdk.util;

import com.google.api.services.bigquery.model.Dataset;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfigurationExtract;
import com.google.api.services.bigquery.model.JobConfigurationLoad;
import com.google.api.services.bigquery.model.JobConfigurationQuery;
import com.google.api.services.bigquery.model.JobConfigurationTableCopy;
import com.google.api.services.bigquery.model.JobReference;
import com.google.api.services.bigquery.model.JobStatistics;
import com.google.api.services.bigquery.model.Table;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.cloud.dataflow.sdk.options.BigQueryOptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.NoSuchElementException;

import javax.annotation.Nullable;

/**
 * An interface for real, mock, or fake implementations of Cloud BigQuery services.
 */
public interface BigQueryServices extends Serializable {

  /**
   * Returns a real, mock, or fake {@link JobService}.
   */
  public JobService getJobService(BigQueryOptions bqOptions);

  /**
   * Returns a real, mock, or fake {@link DatasetService}.
   */
  public DatasetService getDatasetService(BigQueryOptions bqOptions);

  /**
   * Returns a real, mock, or fake {@link BigQueryJsonReader} to read tables.
   */
  public BigQueryJsonReader getReaderFromTable(BigQueryOptions bqOptions, TableReference tableRef);

  /**
   * Returns a real, mock, or fake {@link BigQueryJsonReader} to query tables.
   */
  public BigQueryJsonReader getReaderFromQuery(
      BigQueryOptions bqOptions, String query, String projectId, @Nullable Boolean flatten,
      @Nullable Boolean useLegacySql);

  /**
   * An interface for the Cloud BigQuery load service.
   */
  public interface JobService {
    /**
     * Start a BigQuery load job.
     */
    void startLoadJob(JobReference jobRef, JobConfigurationLoad loadConfig)
        throws InterruptedException, IOException;
    /**
     * Start a BigQuery extract job.
     */
    void startExtractJob(JobReference jobRef, JobConfigurationExtract extractConfig)
        throws InterruptedException, IOException;

    /**
     * Start a BigQuery query job.
     */
    void startQueryJob(JobReference jobRef, JobConfigurationQuery query)
        throws IOException, InterruptedException;

    /**
     * Start a BigQuery copy job.
     */
    void startCopyJob(JobReference jobRef, JobConfigurationTableCopy copyConfig)
        throws IOException, InterruptedException;

    /**
     * Waits for the job is Done, and returns the job.
     *
     * <p>Returns null if the {@code maxAttempts} retries reached.
     */
    Job pollJob(JobReference jobRef, int maxAttempts)
        throws InterruptedException, IOException;

    /**
     * Dry runs the query in the given project.
     */
    JobStatistics dryRunQuery(String projectId, JobConfigurationQuery queryConfig)
        throws InterruptedException, IOException;

    /**
     * Gets the specified {@link Job} by the given {@link JobReference}.
     *
     * Returns null if the job is not found.
     */
    Job getJob(JobReference jobRef) throws IOException, InterruptedException;
  }

  /**
   * An interface to get, create and delete Cloud BigQuery datasets and tables.
   */
  interface DatasetService {
    /**
     * Gets the specified {@link Table} resource by table ID.
     */
    Table getTable(String projectId, String datasetId, String tableId)
        throws InterruptedException, IOException;

    /**
     * Deletes the table specified by tableId from the dataset.
     * If the table contains data, all the data will be deleted.
     */
    void deleteTable(String projectId, String datasetId, String tableId)
        throws IOException, InterruptedException;

    /**
     * Create a {@link Dataset} with the given {@code location} and {@code description}.
     */
    void createDataset(
        String projectId, String datasetId, @Nullable String location, @Nullable String description)
        throws IOException, InterruptedException;

    /**
     * Deletes the dataset specified by the datasetId value.
     *
     * <p>Before you can delete a dataset, you must delete all its tables.
     */
    void deleteDataset(String projectId, String datasetId)
        throws IOException, InterruptedException;
  }

  /**
   * An interface to read the Cloud BigQuery directly.
   */
  public interface BigQueryJsonReader {
    /**
     * Initializes the reader and advances the reader to the first record.
     */
    boolean start() throws IOException;

    /**
     * Advances the reader to the next valid record.
     */
    boolean advance() throws IOException;

    /**
     * Returns the value of the data item that was read by the last {@link #start} or
     * {@link #advance} call. The returned value must be effectively immutable and remain valid
     * indefinitely.
     *
     * <p>Multiple calls to this method without an intervening call to {@link #advance} should
     * return the same result.
     *
     * @throws java.util.NoSuchElementException if {@link #start} was never called, or if
     *         the last {@link #start} or {@link #advance} returned {@code false}.
     */
    TableRow getCurrent() throws NoSuchElementException;

    /**
     * Closes the reader. The reader cannot be used after this method is called.
     */
    void close() throws IOException;
  }
}
