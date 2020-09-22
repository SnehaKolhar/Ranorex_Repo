/*
 * Created by Ranorex
 * User: ffg9ym
 * Date: 22-09-2020
 * Time: 13:07
 * 
 * To change this template use Tools > Options > Coding > Edit standard headers.
 */
using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;
using System.Drawing;
using System.Threading;
using WinForms = System.Windows.Forms;

using Ranorex;
using Ranorex.Core;
using Ranorex.Core.Testing;

namespace SolutionProject.TC_Common
{
    /// <summary>
    /// Description of Fourth.
    /// </summary>
    [TestModule("F514DC83-4D9D-4CE5-887B-EC73802BCE44", ModuleType.UserCode, 1)]
    public class Fourth : ITestModule
    {
        /// <summary>
        /// Constructs a new instance.
        /// </summary>
        public Fourth()
        {
            // Do not delete - a parameterless constructor is required!
        }

        /// <summary>
        /// Performs the playback of actions in this module.
        /// </summary>
        /// <remarks>You should not call this method directly, instead pass the module
        /// instance to the <see cref="TestModuleRunner.Run(ITestModule)"/> method
        /// that will in turn invoke this method.</remarks>
        void ITestModule.Run()
        {
           Report.Success("fourth");
        }
    }
}
