/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sourceforge.htmlunit.xerces.xni;

/**
 * The Augmentations interface defines a table of additional data that may be
 * passed along the document pipeline. The information can contain extra
 * arguments or infoset augmentations, for example PSVI. This additional
 * information is identified by a String key.
 * <p>
 * <strong>Note:</strong> Methods that receive Augmentations are required to
 * copy the information if it is to be saved for use beyond the scope of the
 * method. The Augmentations content is volatile, and maybe modified by any
 * method in any component in the pipeline. Therefore, methods passed this
 * structure should not save any reference to the structure.
 *
 * @author Elena Litani, IBM
 */

public interface Augmentations {
}
