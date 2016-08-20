/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import nl.sogeti.android.gpstracker.integration.ServiceConstants;
import nl.sogeti.android.gpstracker.integration.ServiceManager;

public abstract class BaseTrackPresentor {

    private Context context;
    private BroadcastReceiver receiver;
    private final ServiceManager serviceManager;

    public BaseTrackPresentor() {
        serviceManager = new ServiceManager();
    }

    public synchronized void start(Context context, boolean withService) {
        if (this.context == null) {
            this.context = context;
            if (withService) {
                serviceManager.startup(context, new Runnable() {
                    @Override
                    public void run() {
                        synchronized (BaseTrackPresentor.this) {
                            if (BaseTrackPresentor.this.context != null) {
                                didConnectService(serviceManager);
                            }
                        }
                    }
                });
                registerReceiver();
            }
        }
    }

    public synchronized void stop() {
        if (context != null) {
            unregisterReceiver();
            serviceManager.shutdown(context);
            context = null;
        }
    }

    private void unregisterReceiver() {
        if (receiver != null) {
            context.unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void registerReceiver() {
        unregisterReceiver();
        receiver = new LoggerStateReceiver();
        IntentFilter filter = new IntentFilter(ServiceConstants.LOGGING_STATE_CHANGED_ACTION);
        context.registerReceiver(receiver, filter);
    }

    public Context getContext() {
        return context;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    protected abstract void didConnectService(ServiceManager serviceManager);

    public abstract void didChangeLoggingState(Intent intent);

    private class LoggerStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            didChangeLoggingState(intent);
        }
    }
}
