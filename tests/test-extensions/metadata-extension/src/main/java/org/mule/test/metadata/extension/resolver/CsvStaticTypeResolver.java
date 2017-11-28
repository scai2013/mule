/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.test.metadata.extension.resolver;

import static org.mule.metadata.api.model.MetadataFormat.CSV;

import org.mule.metadata.api.builder.BaseTypeBuilder;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.metadata.resolving.StaticTypeResolver;

public class CsvStaticTypeResolver extends StaticTypeResolver {

  @Override
  public MetadataType getStaticType() {
    return BaseTypeBuilder.create(CSV).objectType().id("csv-object").build();
  }

  @Override
  public String getCategoryName() {
    return "CSV_STATIC";
  }
}
