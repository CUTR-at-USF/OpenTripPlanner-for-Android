/*
 * Copyright 2012 University of South Florida
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package edu.usf.cutr.opentripplanner.android.listeners;

import edu.usf.cutr.opentripplanner.android.model.Server;

/**
 * Interface that is used to list for the event of an OTP
 * server being selected, either manually or automatically
 *
 * @author Khoa Tran
 */

public interface ServerSelectorCompleteListener {

    public void onServerSelectorComplete(Server server);
}
