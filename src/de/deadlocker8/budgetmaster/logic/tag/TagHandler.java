package de.deadlocker8.budgetmaster.logic.tag;

import java.util.ArrayList;

import de.deadlocker8.budgetmaster.logic.Settings;
import de.deadlocker8.budgetmaster.logic.payment.NormalPayment;
import de.deadlocker8.budgetmaster.logic.payment.Payment;
import de.deadlocker8.budgetmaster.logic.payment.RepeatingPaymentEntry;
import de.deadlocker8.budgetmaster.logic.serverconnection.ServerTagConnection;

public class TagHandler
{
	private Settings settings;
	
	public TagHandler(Settings settings)
	{
		this.settings = settings;
	}

	public ArrayList<Tag> getTags(Payment payment) throws Exception
	{
		ArrayList<Tag> tags = new ArrayList<>();
		
		ServerTagConnection connection = new ServerTagConnection(settings);
		
		if(payment instanceof NormalPayment)
		{
			tags.addAll(connection.getAllTagsForPayment((NormalPayment)payment));
		}
		else
		{
			tags.addAll(connection.getAllTagsForRepeatingPayment(((RepeatingPaymentEntry)payment).getRepeatingPaymentID()));
		}
		
		return tags;
	}
	
	public String getTagsAsString(Payment payment) throws Exception
	{
		ArrayList<Tag> tags = getTags(payment);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < tags.size(); i++)
		{
			sb.append(tags.get(i).getName());
			if(i != tags.size()-1)
			{
				sb.append(", ");
			}
		}
		
		return sb.toString();
	}
}